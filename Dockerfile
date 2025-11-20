FROM eclipse-temurin:21-jre
WORKDIR /app

# Kopiuj kod źródłowy
COPY . .

# Zbuduj JAR
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Uruchom aplikację
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/energy-mix-0.0.1-SNAPSHOT.jar"]
