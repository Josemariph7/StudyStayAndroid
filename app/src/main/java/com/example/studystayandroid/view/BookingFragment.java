/*
 * StudyStay © 2024
 *
 * All rights reserved.
 *
 * This software and associated documentation files (the "Software") are owned by StudyStay. Unauthorized copying, distribution, or modification of this Software is strictly prohibited.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this Software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * StudyStay
 * José María Pozo Hidalgo
 * Email: josemariph7@gmail.com
 *
 *
 */

package com.example.studystayandroid.view;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.AccommodationController;
import com.example.studystayandroid.controller.BookingController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.Booking;
import com.example.studystayandroid.model.User;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

/**
 * Fragmento para gestionar las reservas de alojamiento.
 */
public class BookingFragment extends Fragment {

    private TextInputEditText startDateEditText;
    private TextInputEditText endDateEditText;
    private TextView invoiceTextView;
    private RadioGroup paymentRadioGroup;
    private RadioButton fullPaymentRadioButton;
    private RadioButton depositPaymentRadioButton;
    private Button confirmBookingButton;

    private Accommodation accommodation;
    private User currentUser;

    private AccommodationController accommodationController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startDateEditText = view.findViewById(R.id.editTextStartDate);
        endDateEditText = view.findViewById(R.id.editTextEndDate);
        invoiceTextView = view.findViewById(R.id.invoiceTextView);
        paymentRadioGroup = view.findViewById(R.id.paymentRadioGroup);
        fullPaymentRadioButton = view.findViewById(R.id.fullPaymentRadioButton);
        depositPaymentRadioButton = view.findViewById(R.id.depositPaymentRadioButton);
        confirmBookingButton = view.findViewById(R.id.confirmBookingButton);

        accommodationController = new AccommodationController(requireContext());

        startDateEditText.setOnClickListener(v -> showDatePickerDialogStart());
        endDateEditText.setOnClickListener(v -> showDatePickerDialogEnd());

        // Actualizar factura cuando cambia la opción de pago
        paymentRadioGroup.setOnCheckedChangeListener((group, checkedId) -> updateInvoice());

        confirmBookingButton.setOnClickListener(v -> confirmBooking());

        // Recuperar alojamiento y currentUser de los argumentos
        if (getArguments() != null) {
            accommodation = (Accommodation) getArguments().getSerializable("accommodation");
            currentUser = (User) getArguments().getSerializable("currentUser");
            Log.d("BookingFragment", "Accommodation: " + accommodation);
            Log.d("BookingFragment", "Current user: " + currentUser);
        }
    }

    /**
     * Muestra el diálogo de selección de fecha para la fecha de inicio.
     */
    private void showDatePickerDialogStart() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this.requireContext(),
                R.style.CustomDatePickerDialog,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    startDateEditText.setText(selectedDate);
                    updateInvoice();
                },
                year, month, day
        );

        datePickerDialog.setOnShowListener(dialog -> {
            Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
            Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);
            if (positiveButton != null && negativeButton != null) {
                int color = ContextCompat.getColor(getContext(), R.color.SadBlue);
                positiveButton.setTextColor(color);
                negativeButton.setTextColor(color);
            }
        });

        datePickerDialog.show();
    }

    /**
     * Muestra el diálogo de selección de fecha para la fecha de fin.
     */
    private void showDatePickerDialogEnd() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this.requireContext(),
                R.style.CustomDatePickerDialog,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    endDateEditText.setText(selectedDate);
                    updateInvoice();
                },
                year, month, day
        );

        datePickerDialog.setOnShowListener(dialog -> {
            Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
            Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);
            if (positiveButton != null && negativeButton != null) {
                int color = ContextCompat.getColor(getContext(), R.color.SadBlue);
                positiveButton.setTextColor(color);
                negativeButton.setTextColor(color);
            }
        });

        datePickerDialog.show();
    }

    /**
     * Actualiza la factura en función de las fechas seleccionadas y la opción de pago.
     */
    @SuppressLint("DefaultLocale")
    private void updateInvoice() {
        String startDateStr = startDateEditText.getText().toString().trim();
        String endDateStr = endDateEditText.getText().toString().trim();

        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            invoiceTextView.setText("");
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);

            if (endDate.isBefore(startDate)) {
                invoiceTextView.setText("End date must be after start date.");
                return;
            }

            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

            if (daysBetween < 30) {
                invoiceTextView.setText("The booking must be at least one month.");
                return;
            }

            BigDecimal pricePerMonth = accommodation.getPrice();
            long monthsBetween = daysBetween / 30;
            long extraDays = daysBetween % 30;
            BigDecimal dailyCost = pricePerMonth.divide(BigDecimal.valueOf(30), BigDecimal.ROUND_HALF_UP);

            if (fullPaymentRadioButton.isChecked()) {
                // Pago completo
                BigDecimal totalCost = pricePerMonth.multiply(BigDecimal.valueOf(monthsBetween)).add(dailyCost.multiply(BigDecimal.valueOf(extraDays)));
                BigDecimal commission = totalCost.multiply(BigDecimal.valueOf(0.03));
                BigDecimal finalCost = totalCost.add(commission);

                invoiceTextView.setText(String.format(
                        "Invoice:\n" +
                                "------------------------\n" +
                                "Months: %d x €%.2f\n" +
                                "Extra Days: %d x €%.2f\n" +
                                "------------------------\n" +
                                "Subtotal: €%.2f\n" +
                                "Commission (3%%): €%.2f\n" +
                                "------------------------\n" +
                                "Total: €%.2f",
                        monthsBetween, pricePerMonth, extraDays, dailyCost, totalCost, commission, finalCost));
            } else if (depositPaymentRadioButton.isChecked()) {
                // Depósito + Primer mes
                BigDecimal deposit = pricePerMonth;
                BigDecimal firstMonth = pricePerMonth.add(dailyCost.multiply(BigDecimal.valueOf(extraDays)));
                BigDecimal commission = firstMonth.multiply(BigDecimal.valueOf(0.03));
                BigDecimal finalCost = firstMonth.add(commission);

                invoiceTextView.setText(String.format(
                        "Invoice:\n" +
                                "------------------------\n" +
                                "Deposit (1 month): €%.2f\n" +
                                "First Month: €%.2f\n" +
                                "------------------------\n" +
                                "Subtotal: €%.2f\n" +
                                "Commission (3%%): €%.2f\n" +
                                "------------------------\n" +
                                "Total: €%.2f\n" +
                                "Then: €%.2f per month",
                        deposit, firstMonth, firstMonth, commission, finalCost, pricePerMonth));
            }
        } catch (Exception e) {
            invoiceTextView.setText("Invalid date format.");
        }
    }

    /**
     * Confirma la reserva verificando las fechas y la disponibilidad.
     */
    private void confirmBooking() {
        String startDateStr = startDateEditText.getText().toString().trim();
        String endDateStr = endDateEditText.getText().toString().trim();

        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            Toast.makeText(getContext(), "Please select start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);

            if (endDate.isBefore(startDate)) {
                Toast.makeText(getContext(), "End date must be after start date", Toast.LENGTH_SHORT).show();
                return;
            }

            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

            if (daysBetween < 30) {
                Toast.makeText(getContext(), "The booking must be at least one month", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar disponibilidad
            new BookingController(requireContext()).getBookings(new BookingController.BookingListCallback() {
                @Override
                public void onSuccess(List<Booking> bookings) {
                    long activeBookings = bookings.stream()
                            .filter(booking -> booking.getAccommodation().getAccommodationId().equals(accommodation.getAccommodationId())
                                    && !(booking.getEndDate().isBefore(startDate.atStartOfDay()) || booking.getStartDate().isAfter(endDate.atStartOfDay())))
                            .count();

                    if (activeBookings >= accommodation.getCapacity()) {
                        showErrorDialog("The accommodation is not available for the selected dates.");
                    } else {
                        // Simulate payment process and confirm booking
                        simulatePaymentAndConfirmBooking();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error checking availability: " + error, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Simula el proceso de pago y confirma la reserva.
     */
    private void simulatePaymentAndConfirmBooking() {
        // Simular el proceso de pago
        AlertDialog progressDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Processing Payment")
                .setMessage("Redirecting to payment gateway...")
                .setCancelable(false)
                .show();

        // Retraso para simular el proceso de pago
        new Handler().postDelayed(() -> {
            // Cerrar el diálogo de simulación de pago
            progressDialog.dismiss();

            // Crear la reserva
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate startDate = LocalDate.parse(startDateEditText.getText().toString(), formatter);
            LocalDate endDate = LocalDate.parse(endDateEditText.getText().toString(), formatter);

            Booking booking = new Booking(accommodation, currentUser, startDate.atStartOfDay(), endDate.atStartOfDay(), Booking.BookingStatus.PENDING);
            new BookingController(requireContext()).createBooking(booking, new BookingController.BookingCallback() {
                @Override
                public void onSuccess(Object result) {
                    clearForm();

                    // Mostrar diálogo de éxito y redirigir al fragmento de alojamiento
                    showSuccessDialog();
                }

                @Override
                public void onError(String error) {
                    showErrorDialog("Error creating booking: " + error);
                }
            });
        }, 7000); // Retraso aumentado a 7 segundos
    }

    /**
     * Muestra un diálogo de éxito.
     */
    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        @SuppressLint("InflateParams")
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_success, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        dialogTitle.setText("Success");
        dialogMessage.setText("The payment was processed successfully!");

        builder.setView(dialogView);
        AlertDialog successDialog = builder.create();

        buttonConfirm.setOnClickListener(v -> {
            successDialog.dismiss();
            // Redirigir al fragmento de alojamiento
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new AccommodationsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        successDialog.show();
    }

    /**
     * Limpia los campos del formulario.
     */
    private void clearForm() {
        startDateEditText.setText("");
        endDateEditText.setText("");
        invoiceTextView.setText("");
        paymentRadioGroup.clearCheck();
    }

    /**
     * Muestra un diálogo de error.
     *
     * @param message Mensaje de error a mostrar.
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
