package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.persistence.entity.RoleJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.RolePersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.RoleJpaRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoleRepositoryAdapterPaginationTest {
    @Test
    void buildsDatabaseSpecificationForKeywordAndRoleFilters() {
        var adapter = new RoleRepositoryAdapter(mock(RoleJpaRepository.class), mock(RolePersistenceMapper.class));
        var query = new RoleFilterQuery("audit", true, false, false, 0, 10, "name", "ASC");
        Root<RoleJpaEntity> root = mock(Root.class); CriteriaBuilder builder = mock(CriteriaBuilder.class); CriteriaQuery<?> criteria = mock(CriteriaQuery.class);

        adapter.specification(query).toPredicate(root, criteria, builder);

        verify(root).get("name"); verify(root).get("description"); verify(root).get("isDeleted");
        verify(root).get("isActive"); verify(root).get("isSystem");
        verify(builder, times(2)).like(any(), eq("%audit%"), eq('\\'));
        verify(builder).equal(any(), eq(true)); verify(builder).equal(any(), eq(false));
    }

    @Test
    void delegatesPagingSortingToJpaAndReturnsAccurateTotals() {
        RoleJpaRepository jpa = mock(RoleJpaRepository.class); RolePersistenceMapper mapper = mock(RolePersistenceMapper.class);
        RoleJpaEntity entity = new RoleJpaEntity(); Role role = mock(Role.class); when(mapper.toDomain(entity)).thenReturn(role);
        when(jpa.findAll(ArgumentMatchers.<Specification<RoleJpaEntity>>any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(1, 2), 5));
        var adapter = new RoleRepositoryAdapter(jpa, mapper);
        var query = new RoleFilterQuery("audit", true, false, false, 1, 2, "createdAt", "DESC");

        var result = adapter.findPage(query);

        assertEquals(List.of(role), result.content()); assertEquals(5, result.totalElements()); assertEquals(3, result.totalPages());
        assertFalse(result.first()); assertFalse(result.last());
        var pageable = org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(jpa).findAll(ArgumentMatchers.<Specification<RoleJpaEntity>>any(), pageable.capture());
        assertEquals(1, pageable.getValue().getPageNumber()); assertEquals(2, pageable.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("createdAt").getDirection());
        verify(jpa, never()).findAll();
    }
}
