FROM gradle:6.2-jdk11

WORKDIR /app
COPY . /app

RUN gradle build -x test

CMD ["gradle", "build"]
