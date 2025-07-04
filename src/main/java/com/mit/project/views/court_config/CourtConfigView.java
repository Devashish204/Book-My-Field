package com.mit.project.views.court_config;

import com.mit.project.entities.CourtConfig;
import com.mit.project.services.CourtConfigService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.RolesAllowed;

@Route("admin/court-config")
@PageTitle("Court Configuration")
@RolesAllowed("ADMIN")
public class CourtConfigView extends VerticalLayout {

  private final CourtConfigService courtConfigService;
  private final Binder<CourtConfig> binder = new Binder<>(CourtConfig.class);
  private final Grid<CourtConfig> grid = new Grid<>(CourtConfig.class);

  private final TextField courtName = new TextField("Add New Court");
  private final TextField total = new TextField("Total Courts");
  private final TextField hourlyRate = new TextField("Hourly Rate");
  private final ComboBox<String> availability = new ComboBox<>("Availability");

  private CourtConfig editingCourtConfig = null;

  public CourtConfigView(CourtConfigService courtConfigService) {
    this.courtConfigService = courtConfigService;

    setSizeFull();
    setPadding(true);
    setSpacing(true);

    add(buildForm(), buildGrid());
    refreshGrid();
  }

  private Component buildForm() {
    availability.setItems("Available", "Not Available");
    availability.setPlaceholder("Select Availability");
    availability.setClearButtonVisible(true);

    binder
        .forField(courtName)
        .asRequired("Court name is required")
        .bind(CourtConfig::getCourtName, CourtConfig::setCourtName);

    binder
        .forField(total)
        .asRequired("Total is required")
        .withConverter(Integer::valueOf, String::valueOf, "Must be a number")
        .bind(CourtConfig::getTotal, CourtConfig::setTotal);

    binder
        .forField(hourlyRate)
        .asRequired("Hourly rate is required")
        .withConverter(Double::valueOf, String::valueOf, "Must be a number")
        .bind(CourtConfig::getHourlyRate, CourtConfig::setHourlyRate);

    binder
        .forField(availability)
        .asRequired("Availability is required")
        .withConverter(
            value -> "Available".equals(value), bool -> bool ? "Available" : "Not Available")
        .bind(CourtConfig::isAvailable, CourtConfig::setAvailable);

    Button save =
        new Button(
            "Save",
            event -> {
              try {
                if (editingCourtConfig == null) {
                  editingCourtConfig = new CourtConfig();
                }

                binder.writeBean(editingCourtConfig);
                editingCourtConfig.setCourtName(editingCourtConfig.getCourtName().toUpperCase());

                courtConfigService.saveCourtConfig(editingCourtConfig);
                Notification.show("Court config saved", 3000, Notification.Position.TOP_CENTER);
                refreshGrid();
                clearForm();
              } catch (ValidationException e) {
                Notification.show(
                    "Please fix the errors before saving.", 3000, Notification.Position.TOP_CENTER);
              } catch (RuntimeException e) {
                Notification.show(
                    "Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
              }
            });

    Button clear = new Button("Clear", event -> clearForm());

    HorizontalLayout actions = new HorizontalLayout(save, clear);
    FormLayout formLayout = new FormLayout(courtName, total, hourlyRate, availability, actions);
    formLayout.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    return formLayout;
  }

  private Component buildGrid() {
    grid.setColumns("courtName", "total", "hourlyRate", "available");

    grid.addComponentColumn(
            item -> {
              HorizontalLayout actions = new HorizontalLayout();

              Button edit = new Button("Edit");
              edit.addClickListener(
                  e -> {
                    editingCourtConfig = item;
                    binder.readBean(editingCourtConfig);
                  });

              Button delete = new Button("Delete");
              delete.getStyle().set("color", "red");
              delete.addClickListener(
                  e -> {
                    courtConfigService.deleteCourtConfig(item);
                    Notification.show(
                        "Court config deleted", 3000, Notification.Position.TOP_CENTER);
                    refreshGrid();
                  });

              actions.add(edit, delete);
              return actions;
            })
        .setHeader("Actions");

    grid.setSizeFull();
    return grid;
  }

  private void refreshGrid() {
    grid.setItems(courtConfigService.getAllCourtConfigs());
  }

  private void clearForm() {
    editingCourtConfig = null;
    binder.readBean(null);
    courtName.clear();
    total.clear();
    hourlyRate.clear();
    availability.clear();
  }
}
