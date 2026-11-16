package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.domain.dto.query.PermissionFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.persistence.entity.PermissionJpaEntity;
import kh.edu.istad.platform.iam.persistence.mapper.PermissionPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.PermissionJpaRepository;
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

class PermissionRepositoryAdapterPaginationTest {
    @Test
    void buildsSpecificationForKeywordAndPermissionFilters() {
        var adapter = new PermissionRepositoryAdapter(mock(PermissionJpaRepository.class), mock(PermissionPersistenceMapper.class));
        var query = new PermissionFilterQuery("audit", "user", "read", true, false, false, 0, 10, "name", "ASC");
        Root<PermissionJpaEntity> root = mock(Root.class); CriteriaBuilder builder = mock(CriteriaBuilder.class); CriteriaQuery<?> criteria = mock(CriteriaQuery.class);
        adapter.specification(query).toPredicate(root, criteria, builder);
        verify(root).get("name"); verify(root, times(2)).get("resource"); verify(root, times(2)).get("action"); verify(root).get("description");
        verify(root).get("isDeleted"); verify(root).get("isActive"); verify(root).get("isSystem");
        verify(builder, times(4)).like(any(), eq("%audit%"), eq('\\'));
    }

    @Test
    void delegatesPagingSortingAndTotalsToJpa() {
        PermissionJpaRepository jpa = mock(PermissionJpaRepository.class); PermissionPersistenceMapper mapper = mock(PermissionPersistenceMapper.class);
        PermissionJpaEntity entity = new PermissionJpaEntity(); Permission permission = mock(Permission.class); when(mapper.toDomain(entity)).thenReturn(permission);
        when(jpa.findAll(ArgumentMatchers.<Specification<PermissionJpaEntity>>any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(1, 2), 5));
        var adapter = new PermissionRepositoryAdapter(jpa, mapper);
        var query = new PermissionFilterQuery("audit", "user", "read", true, false, false, 1, 2, "createdAt", "DESC");
        var result = adapter.findPage(query);
        assertEquals(List.of(permission), result.content()); assertEquals(5, result.totalElements()); assertEquals(3, result.totalPages());
        assertFalse(result.first()); assertFalse(result.last());
        var pageable = org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(jpa).findAll(ArgumentMatchers.<Specification<PermissionJpaEntity>>any(), pageable.capture());
        assertEquals(1, pageable.getValue().getPageNumber()); assertEquals(2, pageable.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("createdAt").getDirection());
        verify(jpa, never()).findAll();
    }
}
