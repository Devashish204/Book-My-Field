package com.mit.project.views;

import com.mit.project.SecurityService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final H1 viewTitle = new H1();

    public MainLayout(@Autowired SecurityService securityService) {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent(securityService);
    }

    private void addHeaderContent(SecurityService securityService) {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Button logout = new Button("Logout", click -> securityService.logout());

        HorizontalLayout header = new HorizontalLayout(viewTitle, logout);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        addToNavbar(true, toggle, header);
    }

    private void addDrawerContent() {
        Span appName = new Span("Book My Field");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        SideNav nav = createNavigation();

        addToDrawer(header, new Scroller(nav), createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addClassName("custom-sidenav");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));

            if (isAdmin) {
                nav.addItem(new SideNavItem("Admin Dashboard", "admin-dashboard", VaadinIcon.DASHBOARD.create()));
                nav.addItem(new SideNavItem("Court Config", "admin/court-config", VaadinIcon.CALENDAR.create()));
                nav.addItem(new SideNavItem("Earning Report", "admin/earning-report", VaadinIcon.DOLLAR.create()));
                nav.addItem(new SideNavItem("Promotional Emails", "admin/promotions", VaadinIcon.ENVELOPE.create()));
            } else {

                nav.addItem(new SideNavItem("Home", "/user-dashboard", VaadinIcon.HOME.create()));
                nav.addItem(new SideNavItem("Book Court", "new-booking", VaadinIcon.CALENDAR_USER.create()));
            }
        }

        return nav;
    }

    private Footer createFooter() {
        return new Footer();
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        if (getContent() == null) return "";
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title != null ? title.value() : "";
    }
}
