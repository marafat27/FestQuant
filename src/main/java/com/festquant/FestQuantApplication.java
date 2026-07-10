package com.festquant;

import com.festquant.ui.AdminDashboardFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.SwingUtilities;

@SpringBootApplication
public class FestQuantApplication {
    public static void main(String[] args) {
        if (args.length > 0 && "--dashboard".equals(args[0])) {
            SwingUtilities.invokeLater(() -> new AdminDashboardFrame().setVisible(true));
            return;
        }
        SpringApplication.run(FestQuantApplication.class, args);
    }
}
