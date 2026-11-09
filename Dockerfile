FROM python:3.10-slim

# تثبيت التحديثات والحزم المطلوبة مثل FFmpeg
RUN apt-get update && apt-get install -y ffmpeg

# تحديد مجلد العمل داخل الحاوية
WORKDIR /app

# نسخ كافة ملفات المشروع إلى الحاوية
COPY . .

# تثبيت المكتبات المذكورة في ملف requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# الأمر الرئيسي لتشغيل البوت
CMD ["python", "main.py"]
