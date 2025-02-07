plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	id("org.sonarqube") version "6.0.1.5171"
	id("com.github.ben-manes.versions") version "0.51.0"
	id("org.openapi.generator") version "7.10.0"
}

group = "it.gov.pagopa.payhub"
version = "0.0.1"
description = "p4pa-send-notification"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

val springDocOpenApiVersion = "2.7.0"
val openApiToolsVersion = "0.2.6"
val micrometerVersion = "1.4.1"
val bouncycastleVersion = "1.79"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
  implementation("io.micrometer:micrometer-tracing-bridge-otel:$micrometerVersion")
  implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.openapitools:jackson-databind-nullable:$openApiToolsVersion")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
  testAnnotationProcessor("org.projectlombok:lombok")

  //security
  implementation("org.bouncycastle:bcprov-jdk18on:$bouncycastleVersion")

  //	Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito:mockito-core")
	testImplementation ("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required = true
	}
}

val projectInfo = mapOf(
	"artifactId" to project.name,
	"version" to project.version
)

tasks {
	val processResources by getting(ProcessResources::class) {
		filesMatching("**/application.yml") {
			expand(projectInfo)
		}
	}
}

configurations {
	compileClasspath {
		resolutionStrategy.activateDependencyLocking()
	}
}

tasks.compileJava {
  dependsOn("dependenciesBuild")
}

tasks.register("dependenciesBuild") {
  group = "AutomaticallyGeneratedCode"
  description = "grouping all together automatically generate code tasks"

  dependsOn(
    "openApiGenerateP4PASend",
    "openApiGenerateSendClient"
  )
}

configure<SourceSetContainer> {
	named("main") {
		java.srcDir("$projectDir/build/generated/src/main/java")
	}
}

springBoot {
	mainClass.value("it.gov.pagopa.pu.send.SendNotificationApplication")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateP4PASend") {
  group = "openapi"
  description = "description"

  generatorName.set("spring")
  inputSpec.set("$rootDir/openapi/p4pa-send-notification.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.send.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.send.dto.generated")
  configOptions.set(mapOf(
    "dateLibrary" to "java8",
    "requestMappingMode" to "api_interface",
    "useSpringBoot3" to "true",
    "interfaceOnly" to "true",
    "useTags" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)",
    "serializationLibrary" to "jackson"
  ))
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateSendClient") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  inputSpec.set("$rootDir/openapi/send-api-external-b2b-pa-bundle.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.send.connector.send.generated.api")
  modelPackage.set("it.gov.pagopa.pu.send.connector.send.generated.dto")
  modelNameSuffix.set("DTO")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java17",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true"
  ))
  library.set("resttemplate")
}
