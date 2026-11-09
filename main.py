import os
import subprocess
import telebot

# ضع توكن البوت الخاص بك المستخرج من @BotFather
TOKEN = "9133734819:AAHgk-AtRjPYHmkQ0k_QTvg4F7S5AUVn_2c"
bot = telebot.TeleBot(TOKEN)

# متغير لحفظ عملية البث الحالية
stream_process = None

@bot.message_handler(commands=['start', 'help'])
def send_welcome(message):
    msg = (
        "مرحباً بك في بوت إعادة البث المباشر 🚀\n\n"
        "📌 **طريقة الاستخدام:**\n"
        "1. لبدء البث:\n"
        "`/restream <رابط_المصدر> <رابط_السيرفر_Target_RTMP>`\n\n"
        "مثال للبث إلى يوتيوب:\n"
        "`/restream http://example.com/live.m3u8 rtmp://a.rtmp.youtube.com/live2/your-stream-key`\n\n"
        "2. لإيقاف البث الحالي:\n"
        "`/stop`"
    )
    bot.reply_to(message, msg, parse_mode="Markdown")

@bot.message_handler(commands=['restream'])
def start_restream(message):
    global stream_process
    
    if stream_process and stream_process.poll() is None:
        bot.reply_to(message, "⚠️ يوجد بث يعمل حالياً! يرجى إيقافه أولاً باستعمال أمر /stop")
        return

    try:
        # فصل المدخلات: الأمر، رابط المصدر، رابط الهدف
        args = message.text.split(maxsplit=2)
        if len(args) < 3:
            bot.reply_to(message, "❌ **خطأ في الصيغة!**\nالاستخدام الصحيح:\n`/restream <رابط_المصدر> <رابط_RTMP_الهدف>`", parse_mode="Markdown")
            return
            
        input_url = args[1]
        output_rtmp = args[2]

        bot.reply_to(message, "⏳ جاري تشغيل سيرفر البث بواسطة FFmpeg...")

        # أمر FFmpeg لنقل البث مباشرة بدون استهلاك معالج عالي (copy mode)
        cmd = [
            'ffmpeg',
            '-re',
            '-i', input_url,
            '-c', 'copy',
            '-f', 'flv',
            output_rtmp
        ]

        # تشغيل الأمر في الخلفية
        stream_process = subprocess.Popen(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        bot.reply_to(message, "✅ **تم بدء إعادة البث المباشر بنجاح!** 🔴")

    except Exception as e:
        bot.reply_to(message, f"❌ حدث خطأ أثناء تشغيل البث: {str(e)}")

@bot.message_handler(commands=['stop'])
def stop_restream(message):
    global stream_process
    if stream_process and stream_process.poll() is None:
        stream_process.terminate()
        stream_process = None
        bot.reply_to(message, "⏹️ تم إيقاف البث المباشر بنجاح.")
    else:
        bot.reply_to(message, "⚠️ لا يوجد أي بث يعمل حالياً.")

if __name__ == '__main__':
    print("Bot is running...")
    bot.infinity_polling()
