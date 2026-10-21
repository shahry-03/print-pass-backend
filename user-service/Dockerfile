# 1. Base image (Java 21 use kar rahe hain kyunke aapke logs mein Java 21 tha)
FROM eclipse-temurin:17-jre-alpine

# 2. Container ke andar hamara working folder
WORKDIR /app

# 3. Target folder se banne wali .jar file ko container mein copy karo
COPY target/*.jar app.jar

# 4. App ko run karne ki command
ENTRYPOINT ["java", "-jar", "app.jar"]