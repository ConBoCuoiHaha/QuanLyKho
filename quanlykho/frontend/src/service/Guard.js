import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import ApiService from "./ApiService";

export const ProtectedRoute = ({ element: Component }) => {
  const location = useLocation();
  return ApiService.isAuthenticated() ? (
    Component
  ) : (
    <Navigate to="/dang-nhap" replace state={{ from: location }} />
  );
};

export const RoleRoute = ({ element: Component, roles = [] }) => {
  const location = useLocation();
  if (!ApiService.isAuthenticated()) {
    return <Navigate to="/dang-nhap" replace state={{ from: location }} />;
  }
  if (roles.length > 0 && !ApiService.hasAnyRole(roles)) {
    return <Navigate to="/bang-dieu-khien" replace />;
  }
  return Component;
};

export const AdminRoute = ({ element: Component }) => (
  <RoleRoute element={Component} roles={["ADMIN", "MANAGER"]} />
);
