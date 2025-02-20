package com.soloProject.myTrip.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        if (request.getRequestURI().startsWith("/reservation")) {
            // 예약 관련 요청인 경우 세션에 요청 정보 저장
            HttpSession session = request.getSession();
            
            // 예약 정보 맵 생성
            Map<String, Object> reservationInfo = new HashMap<>();
            reservationInfo.put("itemId", request.getParameter("itemId"));
            reservationInfo.put("departureDateTime", request.getParameter("departureDateTime"));
            reservationInfo.put("adultCount", request.getParameter("adultCount"));
            reservationInfo.put("childCount", request.getParameter("childCount"));
            reservationInfo.put("infantCount", request.getParameter("infantCount"));
            
            // 세션에 정보 저장
            session.setAttribute("reservationInfo", reservationInfo);
            session.setAttribute("prevPage", "/reservation/new");
            
            // 로그인 페이지로 리다이렉트
            response.sendRedirect("/member/login");
        } else {
            response.sendRedirect("/member/login");
        }
    }
}
