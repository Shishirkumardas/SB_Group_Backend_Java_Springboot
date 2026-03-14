package org.sb_ibms.repositories;


import org.sb_ibms.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    List<Product> findByCategory(String category);
    List<Product> findByCategoryAndSubCategory(String category, String subCategory);
}
