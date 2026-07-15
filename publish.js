const fs = require('fs');
const readline = require('readline');
const { execSync } = require('child_process');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const ENV_FILE = '.env';
const UPDATE_INFO_FILE = 'update_info.json';
let envParams = {};

if (fs.existsSync(ENV_FILE)) {
    const envContent = fs.readFileSync(ENV_FILE, 'utf-8');
    envContent.split('\n').forEach(line => {
        const [key, ...val] = line.split('=');
        if (key && val) envParams[key.trim()] = val.join('=').trim();
    });
}

function askQuestion(query) {
    return new Promise(resolve => rl.question(query, resolve));
}

async function githubRequest(url, method = 'GET', body = null, isUpload = false) {
    const headers = {
        'Authorization': `Bearer ${envParams.GITHUB_TOKEN}`,
        'Accept': 'application/vnd.github.v3+json',
        'User-Agent': 'Autoelectric-Publish-Script'
    };

    if (isUpload) {
        headers['Content-Type'] = 'application/vnd.android.package-archive';
    } else if (body) {
        headers['Content-Type'] = 'application/json';
    }

    const options = { method, headers };
    if (body) {
        options.body = isUpload ? body : JSON.stringify(body);
    }

    const res = await fetch(url, options);
    if (!res.ok) {
        const errText = await res.text();
        throw new Error(`GitHub API Error (${res.status}): ${errText}`);
    }
    return res.json();
}

async function publish() {
    console.log("=== Автоматическая публикация обновления ===");
    
    if (!envParams.FIREBASE_ADMIN_EMAIL || !envParams.FIREBASE_ADMIN_PASSWORD) {
        console.error("❌ Ошибка: В файле .env не заполнены FIREBASE_ADMIN_EMAIL и FIREBASE_ADMIN_PASSWORD.");
        process.exit(1);
    }
    if (!envParams.GITHUB_TOKEN || !envParams.GITHUB_OWNER || !envParams.GITHUB_REPO) {
        console.error("❌ Ошибка: В файле .env не заполнены параметры GitHub (TOKEN, OWNER, REPO).");
        process.exit(1);
    }

    let newVersionName = "";
    let releaseNotes = "";
    let isAutoHandoff = false;

    // Читаем данные от ИИ, если есть
    if (fs.existsSync(UPDATE_INFO_FILE)) {
        try {
            const info = JSON.parse(fs.readFileSync(UPDATE_INFO_FILE, 'utf8'));
            if (info.versionName && info.releaseNotes) {
                newVersionName = info.versionName;
                releaseNotes = info.releaseNotes;
                isAutoHandoff = true;
                console.log(`🤖 Найден update_info.json! Автоматически применяем версию ${newVersionName}`);
            }
        } catch (e) {
            console.log("⚠️ Не удалось прочитать update_info.json, переходим в ручной режим.");
        }
    }

    if (!isAutoHandoff) {
        newVersionName = await askQuestion("Введите новую версию (например, 1.8.0): ");
        releaseNotes = await askQuestion("Введите описание обновления (Release Notes): ");
    }

    if (!newVersionName || !releaseNotes) {
        console.error("❌ Отменено: Версия и описание не могут быть пустыми.");
        process.exit(1);
    }

    // 1. Обновляем build.gradle.kts
    console.log("Обновляем build.gradle.kts...");
    const gradlePath = 'app/build.gradle.kts';
    let gradleCode = fs.readFileSync(gradlePath, 'utf8');
    
    let newVersionCode = null;
    gradleCode = gradleCode.replace(/versionCode = (\d+)/, (match, p1) => {
        newVersionCode = parseInt(p1) + 1;
        return `versionCode = ${newVersionCode}`;
    });
    gradleCode = gradleCode.replace(/versionName = "(.+?)"/, `versionName = "${newVersionName}"`);
    fs.writeFileSync(gradlePath, gradleCode);
    console.log(`✅ Версия обновлена: Code ${newVersionCode}, Name ${newVersionName}`);

    // 2. Обновляем SettingsScreen.kt (Changelog)
    console.log("Добавляем чейнджлог в интерфейс...");
    const settingsPath = 'app/src/main/java/com/example/autoelectricai/ui/settings/SettingsScreen.kt';
    let settingsCode = fs.readFileSync(settingsPath, 'utf8');
    
    const changelogEntry = `        "v${newVersionName}" to """${releaseNotes}""",\n`;
    settingsCode = settingsCode.replace(/(val changelog = listOf\(\n)/, `$1${changelogEntry}`);
    fs.writeFileSync(settingsPath, settingsCode);
    console.log("✅ Чейнджлог обновлен в приложении.");

    // 3. Сборка APK
    console.log("Запуск Gradle сборки (это может занять пару минут)...");
    try {
        execSync('gradlew.bat assembleDebug', { stdio: 'inherit' });
    } catch (e) {
        console.error("❌ Ошибка сборки!");
        process.exit(1);
    }

    const apkPath = 'app/build/outputs/apk/debug/app-debug.apk';
    if (!fs.existsSync(apkPath)) {
        console.error("❌ APK файл не найден после сборки!");
        process.exit(1);
    }

    // 4. Git Push & Tag
    console.log("Пропускаем Git коммиты (выполним их вручную)...");
    const tagName = `v${newVersionName}`;

    // 5. Создание GitHub Release
    let downloadUrl = "";
    console.log("Создание релиза на GitHub...");
    try {
        const releaseData = await githubRequest(
            `https://api.github.com/repos/${envParams.GITHUB_OWNER}/${envParams.GITHUB_REPO}/releases`,
            'POST',
            {
                tag_name: `v${newVersionName}`,
                name: `Обновление v${newVersionName}`,
                body: releaseNotes,
                draft: false,
                prerelease: false
            }
        );
        console.log("✅ Релиз создан, загружаем APK...");
        
        let uploadUrl = releaseData.upload_url.replace('{?name,label}', `?name=app-debug.apk`);
        
        const apkBuffer = fs.readFileSync(apkPath);
        const assetData = await githubRequest(uploadUrl, 'POST', apkBuffer, true);
        
        downloadUrl = assetData.browser_download_url;
        console.log(`✅ APK успешно прикреплен! Ссылка: ${downloadUrl}`);
    } catch (e) {
        console.error("❌ Ошибка работы с GitHub API:", e);
        process.exit(1);
    }

    // 6. Обновление Firebase
    console.log("Авторизация в Firebase...");
    try {
        const apiKey = "AIzaSyBC-8OqySnHWQqB_AiAZacmGxlDRUqQQNg";
        const authRes = await fetch(`https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${apiKey}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: envParams.FIREBASE_ADMIN_EMAIL,
                password: envParams.FIREBASE_ADMIN_PASSWORD,
                returnSecureToken: true
            })
        });
        const authData = await authRes.json();
        
        if (!authData.idToken) {
            console.error("❌ Ошибка авторизации Firebase:", authData.error?.message);
            process.exit(1);
        }
        
        console.log("Обновление app_updates/latest в Firestore...");
        const firestoreUrl = "https://firestore.googleapis.com/v1/projects/autoelectricai/databases/(default)/documents/app_updates/latest";
        
        const updateDoc = {
            fields: {
                versionCode: { integerValue: newVersionCode.toString() },
                versionName: { stringValue: newVersionName },
                releaseNotes: { stringValue: releaseNotes },
                downloadUrl: { stringValue: downloadUrl }
            }
        };

        const updateRes = await fetch(firestoreUrl, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${authData.idToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updateDoc)
        });
        
        if (updateRes.ok) {
            console.log("🎉 ОГРОМНЫЙ УСПЕХ! Приложение обновлено и новая версия доступна всем пользователям!");
            
            // Удаляем update_info.json после успеха
            if (isAutoHandoff && fs.existsSync(UPDATE_INFO_FILE)) {
                fs.unlinkSync(UPDATE_INFO_FILE);
                console.log("🗑️ Файл update_info.json удален.");
            }
        } else {
            const err = await updateRes.text();
            console.error("❌ Ошибка при записи в Firestore:", err);
        }
    } catch (e) {
        console.error("❌ Сетевая ошибка при работе с Firebase:", e);
    }

    rl.close();
}

publish();
