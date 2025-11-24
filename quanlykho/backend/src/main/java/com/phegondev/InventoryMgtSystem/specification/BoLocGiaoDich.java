package com.phegondev.InventoryMgtSystem.specification;

import com.phegondev.InventoryMgtSystem.models.GiaoDich;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BoLocGiaoDich {

    public static Specification<GiaoDich> byFilter(String searchValue) {
        return (root, query, criteriaBuilder) -> {
            if (searchValue == null || searchValue.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + searchValue.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("note")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("status").as(String.class)), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("transactionType").as(String.class)), searchPattern));

            if (root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("user"))) {
                root.join("user", JoinType.LEFT);
            }
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("user", JoinType.LEFT).get("name")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("user", JoinType.LEFT).get("email")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("user", JoinType.LEFT).get("phoneNumber")), searchPattern));

            if (root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("nhaCungCap"))) {
                root.join("nhaCungCap", JoinType.LEFT);
            }
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("nhaCungCap", JoinType.LEFT).get("name")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("nhaCungCap", JoinType.LEFT).get("contactInfo")), searchPattern));

            if (root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("sanPham"))) {
                root.join("sanPham", JoinType.LEFT);
            }
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("sanPham", JoinType.LEFT).get("name")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("sanPham", JoinType.LEFT).get("sku")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("sanPham", JoinType.LEFT).get("description")), searchPattern));

            if (root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("sanPham")) &&
                    root.join("sanPham").getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("category"))) {
                root.join("sanPham", JoinType.LEFT).join("category", JoinType.LEFT);
            }
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("sanPham", JoinType.LEFT)
                    .join("category", JoinType.LEFT).get("name")), searchPattern));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<GiaoDich> byMonthAndYear(int month, int year) {
        return (root, query, criteriaBuilder) -> {
            Expression<Integer> monthExpression = criteriaBuilder.function("month", Integer.class, root.get("createdAt"));
            Expression<Integer> yearExpression = criteriaBuilder.function("year", Integer.class, root.get("createdAt"));
            Predicate monthPredicate = criteriaBuilder.equal(monthExpression, month);
            Predicate yearPredicate = criteriaBuilder.equal(yearExpression, year);
            return criteriaBuilder.and(monthPredicate, yearPredicate);
        };
    }
}

