FROM gradle:7.3-jdk11

WORKDIR /app
COPY . /app

RUN gradle build -x test

CMD ["gradle", "build"]
