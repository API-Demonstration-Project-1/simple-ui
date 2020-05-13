package com.packagename.myapp.ui;

import org.springframework.beans.factory.annotation.Autowired;
import com.packagename.myapp.data.UserDetails;
import com.packagename.myapp.data.UserDetailsService;
import com.packagename.myapp.data.UserDetailsService.ServiceException;
import com.packagename.myapp.ui.components.AvatarField;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.Route;

/**
 * This is the default (and only) view in this example.
 * <p>
 * It demonstrates how to create a form using Vaadin and the Binder. The backend
 * service and data class are in the <code>.data</code> package.
 */
@Route("")
public class MainView extends VerticalLayout {

    private Checkbox allowMarketingBox;
    private PasswordField passwordField1;
    private PasswordField passwordField2;

    private UserDetailsService service;
    private BeanValidationBinder<UserDetails> binder;

    /**
     * Flag for disabling first run for password validation
     */
    private boolean enablePasswordValidation;

    /**
     * We use Spring to inject the backend into our view
     */
    public MainView(@Autowired UserDetailsService service) {

        this.service = service;

        /*
         * Create the components we'll need
         */

        H3 title = new H3("Login");

        TextField firstnameField = new TextField("user name");
        AvatarField avatarField = new AvatarField("Select Avatar image");

        allowMarketingBox = new Checkbox("Remember me");
        allowMarketingBox.getStyle().set("padding-top", "10px");
        EmailField emailField = new EmailField("Email");
        emailField.setVisible(false);

        passwordField1 = new PasswordField("password");
        passwordField2 = new PasswordField("Password again");

        Span errorMessage = new Span();

        Button submitButton = new Button("Login");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

       
        FormLayout formLayout = new FormLayout(title, firstnameField, passwordField1, 
                allowMarketingBox, errorMessage, submitButton);

        
        formLayout.setMaxWidth("500px");
        formLayout.getStyle().set("margin", "0 auto");

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("490px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

       
        formLayout.setColspan(title, 2);
        formLayout.setColspan(firstnameField, 2);
        formLayout.setColspan(passwordField1, 2);
       // formLayout.setColspan(avatarField, 2);
        formLayout.setColspan(errorMessage, 2);
        formLayout.setColspan(submitButton, 2);

        // Add some styles to the error message to make it pop out
        errorMessage.getStyle().set("color", "var(--lumo-error-text-color)");
        errorMessage.getStyle().set("padding", "15px 0");

        // Add the form to the page
        add(formLayout);

       
        binder = new BeanValidationBinder<UserDetails>(UserDetails.class);
        binder.forField(firstnameField).asRequired().bind("firstname");
        binder.forField(allowMarketingBox).bind("allowsMarketing");
        allowMarketingBox.addValueChangeListener(e -> {
            if (!allowMarketingBox.getValue()) {
                //emailField.setValue("");
            }
        });

        binder.forField(passwordField1).asRequired().withValidator(this::passwordValidator).bind("password");
        passwordField2.addValueChangeListener(e -> {
            enablePasswordValidation = true;
            binder.validate();
        });

        binder.setStatusLabel(errorMessage);

        submitButton.addClickListener(e -> {
            try {

                UserDetails detailsBean = new UserDetails();
                binder.writeBean(detailsBean);
                service.store(detailsBean);
                showSuccess(detailsBean);

            } catch (ValidationException e1) {

            } catch (ServiceException e2) {
            	e2.printStackTrace();
                errorMessage.setText("Saving the data failed, please try again");
            }
        });

    }

    /**
     * We call this method when form submission has succeeded
     */
    private void showSuccess(UserDetails detailsBean) {

    }

   
    private ValidationResult passwordValidator(String pass1, ValueContext ctx) {

       
        if (pass1 == null || pass1.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }

        if (!enablePasswordValidation) {
            enablePasswordValidation = true;
            return ValidationResult.ok();
        }

        String pass2 = passwordField2.getValue();

        if (pass1 != null && pass1.equals(pass2)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("there is a wall here. Will carve a door soon");
    }

    private ValidationResult validateHandle(String handle, ValueContext ctx) {

        String errorMsg = service.validateHandle(handle);

        if (errorMsg == null) {
            return ValidationResult.ok();
        }

        return ValidationResult.error(errorMsg);
    }

    public class VisibilityEmailValidator extends EmailValidator {

        public VisibilityEmailValidator(String errorMessage) {
            super(errorMessage);
        }

        @Override
        public ValidationResult apply(String value, ValueContext context) {

            if (!allowMarketingBox.getValue()) {
                return ValidationResult.ok();
            } else {
                return super.apply(value, context);
            }
        }
    }
}
