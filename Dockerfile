FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dserver.address=0.0.0.0", "target/energy-mix-0.0.1-SNAPSHOT.jar"]
# 1. Dodaj testy w kodzie
# 2. Wrzuć na GitHub:
# git add .
# git commit -m "Add unit tests"
# git push origin main
# 3. Render automatycznie zbuduje nową wersję!