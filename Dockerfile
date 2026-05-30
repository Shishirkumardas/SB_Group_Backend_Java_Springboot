FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8082

CMD ["java", "-jar", "target/sbgroup2-0.0.1-SNAPSHOT.jar"]