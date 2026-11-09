FROM python:3.10-slim

# تثبيت التحديثات والحزم المطلوبة مع أدوات البناء الأساسية
RUN apt-get update && apt-get install -y \
    ffmpeg \
    gcc \
    python3-dev \
    && rm -rf /var/lib/apt/lists/*

# تحديد مجلد العمل
WORKDIR /app

# نسخ الملفات
COPY . .

# تحديث pip وتثبيت المكتبات
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# امر تشغيل البوت
CMD ["python", "main.py"]
