const { Telegraf } = require('telegraf');
const { initializeApp, cert } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const path = require('path');
const fs = require('fs');

// Полученный от пользователя токен
const token = '8227351240:AAGw-trrfL3PckYd8aG5cL-BDnLgzaR9D64';

// Проверяем наличие serviceAccountKey.json
const serviceAccountPath = path.join(__dirname, '..', 'serviceAccountKey.json');
if (!fs.existsSync(serviceAccountPath)) {
    console.error("ОШИБКА: Файл serviceAccountKey.json не найден в корне проекта!");
    process.exit(1);
}

const serviceAccount = require(serviceAccountPath);

// Инициализация Firebase Admin
initializeApp({
  credential: cert(serviceAccount)
});

const db = getFirestore();
const auth = getAuth();

// Инициализация Telegram бота
const bot = new Telegraf(token);

console.log("Бот запущен и ожидает команды /start...");

bot.start(async (ctx) => {
    const payload = ctx.payload; // sessionId, if passed via deep link /start <sessionId>
    const user = ctx.from;

    if (!payload) {
        return ctx.reply("Здравствуйте! Я бот для авторизации в приложении AutoElectric AI.\n\nПожалуйста, используйте кнопку 'Войти через Telegram' внутри самого приложения.");
    }

    const sessionId = payload;
    console.log(`Получен запрос на авторизацию! Session ID: ${sessionId}, User: ${user.username || user.first_name}`);

    try {
        // 1. Создаем уникальный идентификатор для Firebase
        const uid = `tg_${user.id}`;
        
        // 2. Генерируем Custom Token
        const customToken = await auth.createCustomToken(uid);

        // 3. Записываем в Firestore
        const authDocRef = db.collection('telegram_auth').doc(sessionId);
        await authDocRef.set({
          customToken: customToken,
          telegramId: user.id,
          firstName: user.first_name,
          lastName: user.last_name || "",
          username: user.username || "",
          createdAt: FieldValue.serverTimestamp()
        });

        console.log(`Токен успешно сгенерирован и записан для сессии ${sessionId}`);
        
        // 4. Сообщаем пользователю об успехе
        ctx.reply(`✅ Авторизация прошла успешно!\n\nДобро пожаловать, ${user.first_name}.\nТеперь вы можете вернуться в приложение AutoElectric AI.`);
    } catch (error) {
        console.error("Ошибка при генерации токена:", error);
        ctx.reply("❌ Произошла ошибка при попытке авторизации. Попробуйте еще раз позже.");
    }
});

bot.launch();

// Грациозное завершение
process.once('SIGINT', () => bot.stop('SIGINT'));
process.once('SIGTERM', () => bot.stop('SIGTERM'));
