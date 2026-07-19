/**
 * Contains the fest quant application implementation used by FestQuant.
 */
package com.festquant;

import com.festquant.ui.AdminDashboardFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.SwingUtilities;

/**
 * Represents the fest quant application part of the FestQuant application.
 */
@SpringBootApplication
public class FestQuantApplication {
    /**
     * Handles the main step.
     */
    public static void main(String[] args) {
        if (args.length > 0 && "--dashboard".equals(args[0])) {
            SwingUtilities.invokeLater(() -> new AdminDashboardFrame().setVisible(true));
            return;
        }
        SpringApplication.run(FestQuantApplication.class, args);
    }
}
