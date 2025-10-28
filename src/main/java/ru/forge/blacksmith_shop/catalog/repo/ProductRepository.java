package ru.forge.blacksmith_shop.catalog.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.forge.blacksmith_shop.catalog.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Резервируем на складе, не даём уйти в минус
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
