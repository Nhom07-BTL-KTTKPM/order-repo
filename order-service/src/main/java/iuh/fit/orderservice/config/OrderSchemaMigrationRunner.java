package iuh.fit.orderservice.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public OrderSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                ALTER TABLE orders
                    ADD COLUMN IF NOT EXISTS employee_id uuid,
                    ADD COLUMN IF NOT EXISTS discount_amount numeric(19, 2) NOT NULL DEFAULT 0,
                    ADD COLUMN IF NOT EXISTS voucher_id uuid,
                    ADD COLUMN IF NOT EXISTS voucher_code varchar(100),
                    ADD COLUMN IF NOT EXISTS shipping_fee numeric(19, 2) NOT NULL DEFAULT 0,
                    ADD COLUMN IF NOT EXISTS cancel_reason varchar(2000),
                    ADD COLUMN IF NOT EXISTS cancelled_at timestamp,
                    ADD COLUMN IF NOT EXISTS delivered_at timestamp
                """);
    }
}
