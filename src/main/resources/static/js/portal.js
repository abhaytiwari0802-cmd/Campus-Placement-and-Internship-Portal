/**
 * Campus Placement and Internship Portal
 * Frontend interactive utilities
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Auto-dismiss flash alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 6000);
    });

    // 2. Generic confirmation dialog on forms or links with data-confirm attribute
    const confirmElements = document.querySelectorAll('[data-confirm]');
    confirmElements.forEach(function (element) {
        element.addEventListener('click', function (e) {
            const message = element.getAttribute('data-confirm') || 'Are you sure you want to proceed?';
            if (!confirm(message)) {
                e.preventDefault();
                return false;
            }
        });
    });

    // 3. Password match validation on registration forms
    const registerForm = document.querySelector('form.register-form');
    if (registerForm) {
        const passwordInput = registerForm.querySelector('input[name="password"]');
        const confirmPasswordInput = registerForm.querySelector('input[name="confirmPassword"]');
        if (passwordInput && confirmPasswordInput) {
            confirmPasswordInput.addEventListener('input', function () {
                if (passwordInput.value !== confirmPasswordInput.value) {
                    confirmPasswordInput.setCustomValidity('Passwords do not match');
                } else {
                    confirmPasswordInput.setCustomValidity('');
                }
            });
        }
    }

    // 4. Client-side table search filter for pages with quick-filter input
    const searchFilterInput = document.getElementById('tableFilterInput');
    if (searchFilterInput) {
        searchFilterInput.addEventListener('keyup', function () {
            const filterValue = this.value.toLowerCase();
            const tableRows = document.querySelectorAll('.filterable-table tbody tr');
            tableRows.forEach(function (row) {
                const text = row.textContent.toLowerCase();
                if (text.indexOf(filterValue) > -1) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }
});
