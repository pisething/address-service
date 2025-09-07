package com.piseth.java.school.addressservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@Configuration
@EnableReactiveMongoAuditing
public class MongoAuditingConfig {
  // No-op: enables @CreatedDate / @LastModifiedDate
}
