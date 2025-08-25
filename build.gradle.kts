import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.util.*

plugins {
  java
  id("org.springframework.boot") version "3.5.5"
  id("io.spring.dependency-management") version "1.1.7"
  jacoco
  id("org.sonarqube") version "6.2.0.5505"
  id("com.github.ben-manes.versions") version "0.52.0"
  id("org.openapi.generator") version "7.13.0"
  id("org.ajoberstar.grgit") version "5.3.2"
  id("com.gorylenko.gradle-git-properties") version "2.5.0"
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

val springDocOpenApiVersion = "2.8.9"
val openApiToolsVersion = "0.2.6"
val micrometerVersion = "1.5.1"
val bouncycastleVersion = "1.81"
val httpClientVersion = "5.5"
val postgresJdbcVersion = "42.7.7"
val caffeineVersion = "3.2.1"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
  implementation("io.micrometer:micrometer-tracing-bridge-otel:$micrometerVersion")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.openapitools:jackson-databind-nullable:$openApiToolsVersion")
  implementation("org.bouncycastle:bcprov-jdk18on:$bouncycastleVersion")
  implementation("org.postgresql:postgresql:$postgresJdbcVersion")
  implementation("org.apache.httpcomponents.client5:httpclient5:$httpClientVersion")

  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
  testAnnotationProcessor("org.projectlombok:lombok")

  //	Testing
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.projectlombok:lombok")
  testImplementation("com.h2database:h2")
}

tasks.withType<Test> {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)
}

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
  mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}
tasks {
  test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
  }
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
    "openApiGenerateSendClient",
    "openApiGenerateORGANIZATION",
    "openApiGenerateWORKFLOWHUB",
    "openApiGenerateDEBTPOSITIONS",
    "openApiGeneratePDND"
  )
}

configure<SourceSetContainer> {
  named("main") {
    java.srcDir("$projectDir/build/generated/src/main/java")
  }
}

springBoot {
  buildInfo()
  mainClass.value("it.gov.pagopa.pu.send.SendNotificationApplication")
}

tasks.register<GenerateTask>("openApiGenerateP4PASend") {
  group = "openapi"
  description = "description"

  generatorName.set("spring")
  inputSpec.set("$rootDir/openapi/p4pa-send-notification.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.send.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.send.dto.generated")
  typeMappings.set(mapOf(
    "NotificationStatus" to "it.gov.pagopa.pu.send.enums.NotificationStatus",
    "NotificationPriceResponseV23DTO" to "it.gov.pagopa.pu.send.connector.send.generated.dto.NotificationPriceResponseV23DTO",
    "ProgressResponseElementV25DTO" to "it.gov.pagopa.pu.send.connector.send.generated.dto.ProgressResponseElementV25DTO",
  ))
  configOptions.set(mapOf(
    "dateLibrary" to "java8",
    "requestMappingMode" to "api_interface",
    "useSpringBoot3" to "true",
    "interfaceOnly" to "true",
    "useTags" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)",
    "serializationLibrary" to "jackson",
    "serializableModel" to "true"
  ))
}

var targetEnv = when (Objects.requireNonNullElse(System.getProperty("targetBranch"), grgit.branch.current().name)) {
  "uat" -> "uat"
  "main" -> "main"
  else -> "develop"
}

tasks.register<GenerateTask>("openApiGenerateSendClient") {
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
    "dateLibrary" to "java8",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useOneOfInterfaces" to "false",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "serializableModel" to "true"
  ))
  library.set("resttemplate")
}

tasks.register<GenerateTask>("openApiGenerateORGANIZATION") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-organization/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.organization.generated")
  apiPackage.set("it.gov.pagopa.pu.organization.client.generated")
  modelPackage.set("it.gov.pagopa.pu.organization.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useOneOfInterfaces" to "true",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
  ))
  library.set("resttemplate")
}

tasks.register<GenerateTask>("openApiGenerateWORKFLOWHUB") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-workflow-hub/refs/heads/$targetEnv/openapi/p4pa-workflow-hub.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.workflowhub.generated")
  apiPackage.set("it.gov.pagopa.pu.workflowhub.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.workflowhub.dto.generated")
  typeMappings.set(mapOf(
    "DebtPositionDTO" to "it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO",
    "IngestionFlowFileType" to "String",
    "WfExecutionConfig" to "String",
    "ExportFileType" to "String",
    "WorkflowTypeOrg" to "String",
    "WorkflowExecutionStatus" to "String",
    "ScheduleEnum" to "String"
  ))
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useOneOfInterfaces" to "true",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
  ))
  library.set("resttemplate")
}

tasks.register<GenerateTask>("openApiGenerateDEBTPOSITIONS") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-debt-positions/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.debtposition.generated")
  apiPackage.set("it.gov.pagopa.pu.debtposition.client.generated")
  modelPackage.set("it.gov.pagopa.pu.debtposition.dto.generated")
  typeMappings.set(mapOf("LocalDateTime" to "java.time.LocalDateTime"))
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useOneOfInterfaces" to "true",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
  ))
  library.set("resttemplate")
}

tasks.register<GenerateTask>("openApiGeneratePDND") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-pdnd-services/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.pdnd.generated")
  apiPackage.set("it.gov.pagopa.pu.pdnd.client.generated")
  modelPackage.set("it.gov.pagopa.pu.pdnd.dto.generated")
  typeMappings.set(mapOf("LocalDateTime" to "java.time.LocalDateTime"))
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useOneOfInterfaces" to "true",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
  ))
  library.set("resttemplate")
}
