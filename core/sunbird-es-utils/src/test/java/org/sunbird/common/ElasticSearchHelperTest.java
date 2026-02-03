package org.sunbird.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.sunbird.dto.SearchDTO;
import org.sunbird.keys.JsonKey;

public class ElasticSearchHelperTest {

  @Test
  public void testGetSortOrder() {
    assertEquals(SortOrder.ASC, ElasticSearchHelper.getSortOrder("ASC"));
    assertEquals(SortOrder.ASC, ElasticSearchHelper.getSortOrder("asc"));
    assertEquals(SortOrder.DESC, ElasticSearchHelper.getSortOrder("DESC"));
    assertEquals(SortOrder.DESC, ElasticSearchHelper.getSortOrder("desc"));
    assertEquals(SortOrder.DESC, ElasticSearchHelper.getSortOrder("invalid"));
  }

  @Test
  public void testCreateMatchQuery() {
    MatchQueryBuilder query = ElasticSearchHelper.createMatchQuery("fieldName", "value", 1.5f);
    assertNotNull(query);
    assertEquals("fieldName", query.fieldName());
    assertEquals("value", query.value());
  }

  @Test
  public void testCreateMatchQueryWithoutBoost() {
    MatchQueryBuilder query = ElasticSearchHelper.createMatchQuery("fieldName", "value", null);
    assertNotNull(query);
    assertEquals("fieldName", query.fieldName());
    assertEquals("value", query.value());
  }

  @Test
  public void testGetConstraints() {
    SearchDTO searchDTO = new SearchDTO();
    Map<String, Integer> softConstraints = new HashMap<>();
    softConstraints.put("field1", 10);
    softConstraints.put("field2", 5);
    searchDTO.setSoftConstraints(softConstraints);

    Map<String, Float> constraints = ElasticSearchHelper.getConstraints(searchDTO);
    assertNotNull(constraints);
    assertEquals(2, constraints.size());
    assertEquals(10.0f, constraints.get("field1"), 0.001);
    assertEquals(5.0f, constraints.get("field2"), 0.001);
  }

  @Test
  public void testGetConstraintsEmpty() {
    SearchDTO searchDTO = new SearchDTO();
    Map<String, Float> constraints = ElasticSearchHelper.getConstraints(searchDTO);
    assertNotNull(constraints);
    assertTrue(constraints.isEmpty());
  }

  @Test
  public void testCalculateEndTime() {
    long startTime = System.currentTimeMillis();
    long endTime = ElasticSearchHelper.calculateEndTime(startTime);
    assertTrue(endTime >= 0);
  }

  @Test
  public void testCreateSearchDTO() {
    Map<String, Object> searchQueryMap = new HashMap<>();
    searchQueryMap.put(JsonKey.QUERY, "test query");
    searchQueryMap.put(JsonKey.LIMIT, 20);
    searchQueryMap.put(JsonKey.OFFSET, 5);

    List<String> fields = new ArrayList<>();
    fields.add("field1");
    searchQueryMap.put(JsonKey.FIELDS, fields);

    SearchDTO searchDTO = ElasticSearchHelper.createSearchDTO(searchQueryMap);

    assertNotNull(searchDTO);
    assertEquals("test query", searchDTO.getQuery());
    assertEquals((Integer) 20, searchDTO.getLimit());
    assertEquals((Integer) 5, searchDTO.getOffset());
    assertEquals(fields, searchDTO.getFields());
  }

  @Test
  public void testCreateSearchDTOWithBigInteger() {
      Map<String, Object> searchQueryMap = new HashMap<>();
      searchQueryMap.put(JsonKey.LIMIT, BigInteger.valueOf(20));
      searchQueryMap.put(JsonKey.OFFSET, BigInteger.valueOf(5));

      SearchDTO searchDTO = ElasticSearchHelper.createSearchDTO(searchQueryMap);

      assertNotNull(searchDTO);
      assertEquals((Integer) 20, searchDTO.getLimit());
      assertEquals((Integer) 5, searchDTO.getOffset());
  }

  @Test
  public void testCreateLexicalQueryStartsWith() {
    Map<String, Object> operation = new HashMap<>();
    operation.put(ElasticSearchHelper.STARTS_WITH, "prefix");
    QueryBuilder query = ElasticSearchHelper.createLexicalQuery("field", operation, null);
    assertNotNull(query);
    assertTrue(query.toString().contains("prefix"));
  }

  @Test
  public void testCreateLexicalQueryEndsWith() {
    Map<String, Object> operation = new HashMap<>();
    operation.put(ElasticSearchHelper.ENDS_WITH, "suffix");
    QueryBuilder query = ElasticSearchHelper.createLexicalQuery("field", operation, null);
    assertNotNull(query);
    assertTrue(query.toString().contains("~suffix"));
  }
}
