package ml.empee.json.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import ml.empee.json.validator.annotations.RegEx;
import ml.empee.json.validator.annotations.Required;
import ml.empee.json.validator.annotations.Validator;

public final class Validation {

  public static void validate(Object object) {
    if (object.getClass().isArray()) {
      for (Object o : (Object[]) object) {
        validate(o);
      }
    } else {
      validateRequired(object);
      validateRegEx(object);
      executeValidators(object);
    }
  }

  @SneakyThrows
  private static void executeValidators(Object object) {
    List<Method> methods = findAllMethodsWithAnnotation(object, Validator.class);
    for (Method method : methods) {
      method.setAccessible(true);
      method.invoke(object);
    }
  }

  @SneakyThrows
  private static void validateRegEx(Object object) {
    List<Field> fields = findAllFieldsWithAnnotation(object, RegEx.class);
    for (Field field : fields) {
      if (field.getType() == String.class) {

        field.setAccessible(true);
        String value = (String) field.get(object);
        if (value != null) {
          RegEx annotation = field.getAnnotation(RegEx.class);
          if (!value.matches(annotation.value())) {
            throw new IllegalArgumentException(
                "Field " + field.getName() + " does not match the regex " + annotation.value());
          }
        }

      }
    }
  }

  @SneakyThrows
  private static void validateRequired(Object object) {
    List<Field> fields = findAllFieldsWithAnnotation(object, Required.class);
    for (Field field : fields) {
      field.setAccessible(true);
      Objects.requireNonNull(field.get(object), "Field " + field.getName() + " is required");
    }
  }

  private static List<Field> findAllFieldsWithAnnotation(Object object, Class<? extends Annotation> annotation) {
    return Arrays.stream(object.getClass().getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(annotation))
        .collect(Collectors.toList());
  }

  private static List<Method> findAllMethodsWithAnnotation(Object object, Class<? extends Annotation> annotation) {
    return Arrays.stream(object.getClass().getDeclaredMethods())
        .filter(field -> field.isAnnotationPresent(annotation))
        .collect(Collectors.toList());
  }

}