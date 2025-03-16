plugins {
	java
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.netflix.dgs.codegen") version "7.0.3"
}

group = "solutions.onz.services"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(23)
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

extra["netflixDgsVersion"] = "10.0.4"

dependencies {
	implementation("org.springframework.integration:spring-integration-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-graphql")
	implementation("org.springframework.boot:spring-boot-starter-integration")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.integration:spring-integration-mail")
	implementation("org.springframework.security:spring-security-messaging")
	implementation("org.springframework.session:spring-session-core")
	implementation("org.springframework.boot:spring-boot-starter-undertow")
	implementation("org.springframework.integration:spring-integration-redis")
	implementation("org.springframework.session:spring-session-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

	implementation("jakarta.annotation:jakarta.annotation-api")

	implementation("org.apache.commons:commons-lang3")

	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("io.micrometer:micrometer-registry-prometheus-simpleclient")

	implementation("org.openpnp:opencv:4.9.0-0")

	implementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter")

	implementation(platform("io.mongock:mongock-bom:5.5.0"))
	implementation("io.mongock:mongock-springboot-v3")
	implementation("io.mongock:mongodb-reactive-driver")
	implementation("io.mongock:mongodb-springdata-v4-driver")

	implementation("org.springdoc:springdoc-openapi-starter-webflux-api:2.6.0")

	implementation("com.github.ulisesbocchio:jasypt-spring-boot:3.0.5")

	implementation(platform("com.azure.spring:spring-cloud-azure-dependencies:5.20.1"))
	implementation("com.azure.spring:spring-cloud-azure-starter")
	implementation("com.azure.spring:spring-cloud-azure-starter-keyvault")



	implementation("com.azure.spring:spring-cloud-azure-starter-actuator")


	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.batch:spring-batch-test")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testImplementation("org.springframework.integration:spring-integration-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.projectreactor.tools:blockhound-junit-platform:1.0.10.RELEASE")

	testImplementation("com.tngtech.archunit:archunit-junit5-api:1.3.0") {
		exclude(group = "org.slf4j", module = "slf4j-api")
	}
	testRuntimeOnly("com.tngtech.archunit:archunit-junit5-engine:1.3.0") {
		exclude(group = "org.slf4j", module = "slf4j-api")
	}

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	modules {
		module("org.springframework.boot:spring-boot-starter-tomcat") {
			replacedBy("org.springframework.boot:spring-boot-starter-undertow", "Use Undertow instead of Tomcat, as its bit faster")
		}
	}
}

dependencyManagement {
	imports {
		mavenBom("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${property("netflixDgsVersion")}")
	}
}

tasks.generateJava {
	schemaPaths.add("${projectDir}/src/main/resources/graphql-client")
	packageName = "solutions.onz.services.imagengine.codegen"
	generateClient = true
}

tasks.withType<Test> {
	useJUnitPlatform()
	exclude( "**/*IT*", "**/*IntTest*")
	testLogging {
		events("passed", "skipped", "failed")
		showExceptions = true
		showCauses = true
	}

}
