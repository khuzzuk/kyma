package net.kyma.dm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DataQuery {
   private List<Parameter> parameters;

   public static DataQuery queryFor(SupportedField field, String value, boolean isWildcard) {
      DataQuery dataQuery = new DataQuery(new ArrayList<>());
      dataQuery.and(field, value, isWildcard);
      return dataQuery;
   }

   public static DataQuery newQuery() {
      return new DataQuery(new ArrayList<>());
   }

   public DataQuery and(SupportedField field, String value, boolean isWildCard) {
      parameters.add(new Parameter(isWildCard, field, value));
      return this;
   }

   public static DataQuery empty() {
      return new DataQuery(Collections.emptyList());
   }

   public boolean hasParameters() {
      return parameters.isEmpty();
   }

   @AllArgsConstructor(access = AccessLevel.PRIVATE)
   @Getter
   public static class Parameter {
      private boolean wildCard;
      private SupportedField field;
      private String value;
   }
}
