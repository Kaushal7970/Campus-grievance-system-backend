FROM eclipse-temurin:21 AS build

WORKDIR /workspace

# Copy wrapper + pom first for better Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
	&& ./mvnw -q -DskipTests dependency:go-offline

# Copy sources last
COPY src/ src/

RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]