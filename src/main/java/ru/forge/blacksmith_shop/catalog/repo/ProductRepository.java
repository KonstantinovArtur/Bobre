package ru.forge.blacksmith_shop.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.forge.blacksmith_shop.catalog.domain.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Поиск + фильтр по категории (без fetch, в шаблоне используем p.categoryName)
    @Query("""
           select p
             from Product p
             left join p.category c
            where (:q is null or :q = '' or lower(p.name) like lower(concat('%', :q, '%')))
              and (:catId is null or c.id = :catId)
            order by p.id desc
           """)
    List<Product> search(@Param("q") String q, @Param("catId") Integer categoryId);

    // Резерв на складе
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Product p
              set p.stockQty = p.stockQty - :qty
            where p.id = :id
              and p.stockQty >= :qty
           """)
    int tryReserveStock(@Param("id") Integer id, @Param("qty") int qty);

    // Возврат резерва
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Product p
              set p.stockQty = p.stockQty + :qty
            where p.id = :id
           """)
    int releaseStock(@Param("id") Integer id, @Param("qty") int qty);
}
