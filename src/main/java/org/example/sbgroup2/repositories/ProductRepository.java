package org.example.sbgroup2.repositories;


import org.example.sbgroup2.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    List<Product> findByCategory(String category);

    List<Product> findByCategoryAndSubCategory(String category, String subCategory);

//    List<Product> findByDeletedFalse();
}
