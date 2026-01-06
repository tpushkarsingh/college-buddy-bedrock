package com.college.buddy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Slf4j
public class DatabaseInitializer implements BeanPostProcessor {

    /**
     * This ensures that as soon as the DataSource is created, we run the
     * CREATE EXTENSION command. This happens BEFORE any other beans (like
     * VectorStore)
     * try to use the database.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource dataSource) {
            try {
                log.info("DATABASE-INIT: DataSource '{}' detected. Running pre-initialization SQL...", beanName);
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector;");
                log.info("DATABASE-INIT: pgvector extension ensured.");
            } catch (Exception e) {
                // We log as WARN because it might fail if the user really has ZERO permissions,
                // but usually, the DB owner can run this.
                log.warn("DATABASE-INIT: Failed to auto-enable pgvector. Error: {}", e.getMessage());
            }
        }
        return bean;
    }
}
