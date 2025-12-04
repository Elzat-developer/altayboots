package altay.boots.altayboots.repository;


import altay.boots.altayboots.model.entity.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogRepo extends JpaRepository<Catalog,Integer> {
    Catalog findById (int catalogId);
}
