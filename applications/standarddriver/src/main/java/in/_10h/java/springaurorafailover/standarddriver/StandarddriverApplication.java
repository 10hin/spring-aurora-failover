package in._10h.java.springaurorafailover.standarddriver;

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.contrib.awsxray.AwsXrayIdGenerator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.extension.aws.AwsXrayPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@SpringBootApplication
@Configuration
public class StandarddriverApplication {

	public static void main(String[] args) {
		Security.setProperty("networkaddress.cache.ttl", "1");
		Security.setProperty("networkaddress.cache.negative.ttl", "1");
		SpringApplication.run(StandarddriverApplication.class, args);
	}

	@Bean
	public OpenTelemetrySdk openTelemetrySdk() {
		return OpenTelemetrySdk.builder()

				// This will enable your downstream requests to include the X-Ray trace header
				.setPropagators(
						ContextPropagators.create(
								TextMapPropagator.composite(
										W3CTraceContextPropagator.getInstance(), AwsXrayPropagator.getInstance())))

				// This provides basic configuration of a TracerProvider which generates X-Ray compliant IDs
				.setTracerProvider(
						SdkTracerProvider.builder()
								.addSpanProcessor(
										BatchSpanProcessor.builder(OtlpGrpcSpanExporter.getDefault()).build())
								.setIdGenerator(AwsXrayIdGenerator.getInstance())
								.build())
				.buildAndRegisterGlobal();
	}

}
