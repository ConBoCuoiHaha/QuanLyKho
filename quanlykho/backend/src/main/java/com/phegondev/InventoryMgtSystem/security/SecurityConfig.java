package com.phegondev.InventoryMgtSystem.security;

import com.phegondev.InventoryMgtSystem.exceptions.CustomAccessDenialHandler;
import com.phegondev.InventoryMgtSystem.exceptions.CustomAuthenticationEntryPoint;
import com.phegondev.InventoryMgtSystem.enums.QuyenTacVu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final AuthFilter authFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDenialHandler customAccessDenialHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDenialHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**").hasAuthority(QuyenTacVu.DASHBOARD_READ.name())
                        .requestMatchers(HttpMethod.GET, "/api/bao-cao/**").hasAuthority(QuyenTacVu.BAO_CAO_READ.name())
                        .requestMatchers(HttpMethod.GET, "/api/xuat-bao-cao/**").hasAuthority(QuyenTacVu.EXCEL_EXPORT.name())
                        .requestMatchers(HttpMethod.GET, "/api/xuat-excel/**").hasAuthority(QuyenTacVu.EXCEL_EXPORT.name())
                        .requestMatchers(HttpMethod.GET, "/api/xuat-pdf/**").hasAuthority(QuyenTacVu.PDF_EXPORT.name())
                        .requestMatchers(HttpMethod.POST, "/api/nhap-excel/**").hasAuthority(QuyenTacVu.EXCEL_IMPORT.name())

                        .requestMatchers(HttpMethod.GET, "/api/san-pham/**").hasAuthority(QuyenTacVu.SAN_PHAM_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/san-pham/**").hasAuthority(QuyenTacVu.SAN_PHAM_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/san-pham/**").hasAuthority(QuyenTacVu.SAN_PHAM_UPDATE.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/san-pham/**").hasAuthority(QuyenTacVu.SAN_PHAM_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/san-pham/**").hasAuthority(QuyenTacVu.SAN_PHAM_DELETE.name())

                        .requestMatchers(HttpMethod.GET, "/api/danh-muc/**").hasAuthority(QuyenTacVu.DANH_MUC_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/danh-muc/**").hasAuthority(QuyenTacVu.DANH_MUC_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/danh-muc/**").hasAuthority(QuyenTacVu.DANH_MUC_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/danh-muc/**").hasAuthority(QuyenTacVu.DANH_MUC_DELETE.name())

                        .requestMatchers(HttpMethod.GET, "/api/nha-cung-cap/**").hasAuthority(QuyenTacVu.NHA_CUNG_CAP_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/nha-cung-cap/**").hasAuthority(QuyenTacVu.NHA_CUNG_CAP_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/nha-cung-cap/**").hasAuthority(QuyenTacVu.NHA_CUNG_CAP_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/nha-cung-cap/**").hasAuthority(QuyenTacVu.NHA_CUNG_CAP_DELETE.name())

                        .requestMatchers(HttpMethod.GET, "/api/khach-hang/**").hasAuthority(QuyenTacVu.KHACH_HANG_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/khach-hang/**").hasAuthority(QuyenTacVu.KHACH_HANG_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/khach-hang/**").hasAuthority(QuyenTacVu.KHACH_HANG_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/khach-hang/**").hasAuthority(QuyenTacVu.KHACH_HANG_DELETE.name())

                        .requestMatchers(HttpMethod.GET, "/api/giao-dich/**").hasAuthority(QuyenTacVu.GIAO_DICH_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/giao-dich/**").hasAuthority(QuyenTacVu.GIAO_DICH_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/giao-dich/**").hasAuthority(QuyenTacVu.GIAO_DICH_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/giao-dich/**").hasAuthority(QuyenTacVu.GIAO_DICH_DELETE.name())

                        .requestMatchers(HttpMethod.GET, "/api/don-ban-hang/**").hasAuthority(QuyenTacVu.DON_BAN_HANG_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/don-ban-hang/**").hasAuthority(QuyenTacVu.DON_BAN_HANG_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/don-ban-hang/**").hasAuthority(QuyenTacVu.DON_BAN_HANG_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/don-ban-hang/**").hasAuthority(QuyenTacVu.DON_BAN_HANG_DELETE.name())

                        .requestMatchers(HttpMethod.GET, "/api/don-dat-hang/**").hasAuthority(QuyenTacVu.DON_DAT_HANG_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/don-dat-hang/**").hasAuthority(QuyenTacVu.DON_DAT_HANG_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/don-dat-hang/**").hasAuthority(QuyenTacVu.DON_DAT_HANG_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/don-dat-hang/**").hasAuthority(QuyenTacVu.DON_DAT_HANG_DELETE.name())

                        .requestMatchers(HttpMethod.GET, "/api/kho/**").hasAuthority(QuyenTacVu.KHO_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/kho/**").hasAuthority(QuyenTacVu.KHO_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/kho/**").hasAuthority(QuyenTacVu.KHO_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/kho/**").hasAuthority(QuyenTacVu.KHO_DELETE.name())

                        .requestMatchers(HttpMethod.GET, "/api/kiem-ke/**").hasAuthority(QuyenTacVu.KIEM_KE_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/kiem-ke/**").hasAuthority(QuyenTacVu.KIEM_KE_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/kiem-ke/**").hasAuthority(QuyenTacVu.KIEM_KE_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/kiem-ke/**").hasAuthority(QuyenTacVu.KIEM_KE_DELETE.name())

                        .requestMatchers(HttpMethod.GET, "/api/lo-hang/**").hasAuthority(QuyenTacVu.LO_HANG_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/lo-hang/**").hasAuthority(QuyenTacVu.LO_HANG_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/lo-hang/**").hasAuthority(QuyenTacVu.LO_HANG_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/lo-hang/**").hasAuthority(QuyenTacVu.LO_HANG_DELETE.name())

                        .requestMatchers("/api/nhat-ky/**").hasAuthority(QuyenTacVu.NHAT_KY_READ.name())
                        .requestMatchers("/api/barcode/**").hasAuthority(QuyenTacVu.MA_VACH_READ.name())

                        .anyRequest().authenticated()
                )
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
