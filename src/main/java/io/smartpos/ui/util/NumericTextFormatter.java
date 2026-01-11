/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.ui.util;

import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;

public class NumericTextFormatter {

    public static TextFormatter<String> integerOnly() {

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        };

        return new TextFormatter<>(filter);
    }

    public static TextFormatter<String> decimalOnly() {

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            if (newText.matches("\\d*(\\.\\d*)?")) {
                return change;
            }
            return null;
        };

        return new TextFormatter<>(filter);
    }
}
