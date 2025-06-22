package com.dd3ok.publicdataexporter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableBatchProcessing
public class PublicDataExporterApplication {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(PublicDataExporterApplication.class, args);

        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job job = context.getBean("csvToDbJob", Job.class);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("launchTime", LocalDateTime.now())
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
    }
}