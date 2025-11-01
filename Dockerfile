# Etapa 1: Build da aplicação
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia apenas o pom.xml e baixa as dependências para cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte da aplicação
COPY src ./src

# Faz o build da aplicação(sem rodar os testes)
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final para rodar a aplicação
FROM eclipse-temurin:21-jre-alpine

# Define diretório de trabalho
WORKDIR /app

# Copia o JAR gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta 8080
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
