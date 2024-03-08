package io.github.burukeyou.dataframe.dataframe;

import io.github.burukeyou.dataframe.IFrame;
import io.github.burukeyou.dataframe.dataframe.item.FT2;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

@Getter
public abstract class AbstractCommonFrame<T> implements IFrame<T> {

    protected static final String MSG = "****";

    protected List<String> fieldList = new ArrayList<>();


    protected String[][] buildPrintDataArr(int limit) {
        List<T> dataList = toLists();
        if (dataList.isEmpty()){
            return null;
        }
        List<String> filedList = getFieldList();
        int rowLen = dataList.size() + 1;
        int colLen = filedList.size() * 2 + 1;

        String[][] dataArr = new String[rowLen][colLen];


        int index1 = 0;
        for (String field : filedList) {
            dataArr[0][index1++] = field;
            dataArr[0][index1++] = MSG;
        }
        dataArr[0][index1] = "\n";

        index1 = 0;
        if (dataList.isEmpty()) {
            for (String field : filedList) {
                dataArr[1][index1++] = "";
                dataArr[1][index1++] = MSG;
            }
        }

        int row = 1;
        for (T t : dataList) {
            int tmpIndex = 0;
            for (String fieldName : filedList) {
                try {
                    Field field = t.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object o = field.get(t);
                    dataArr[row][tmpIndex++] = o == null ? "" : o.toString();
                    dataArr[row][tmpIndex++] = MSG;
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            dataArr[row][tmpIndex] = "\n";
            row++;
        }

        // 格式对齐
        for (int i = 0; i < colLen -1; i++) {
            int maxStrLen = -1;
            for (int j = 0; j < rowLen; j++) {
                if (Objects.equals(dataArr[j][i], MSG)) {
                    continue;
                }
                if (dataArr[j][i].length() > maxStrLen) {
                    maxStrLen = dataArr[j][i].length();
                }
            }
            if (maxStrLen != -1) {
                for (int j = 0; j < rowLen; j++) {
                    int need = maxStrLen - dataArr[j][i].length();
                    if (need > 0 ) {
                        dataArr[j][i] = dataArr[j][i] + getSpace(need);
                    }
                }
            }
        }
        return dataArr;
    }

    private String getSpace(int need) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < need; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    protected Type[] getSuperClassActualTypeArguments(Class<?> clz){
        Type superclass = clz.getGenericSuperclass();
        if (superclass instanceof ParameterizedType){
            return  ((ParameterizedType)superclass).getActualTypeArguments();
        }
        return null;
    }

    protected Type[] getSuperInterfaceActualTypeArguments(Class<?> clz){
        Type[] genericInterfaces = clz.getGenericInterfaces();
        if (genericInterfaces[0] instanceof ParameterizedType){
            return  ((ParameterizedType)genericInterfaces[0]).getActualTypeArguments();
        }
        return null;
    }

    protected static <R extends Number> R bigDecimalToClassValue(BigDecimal value, Class<R> valueClass) {
        if (value == null) {
            return null;
        }
        if (BigDecimal.class.equals(valueClass)){
            return (R)value;
        }
        else if (Byte.class.equals(valueClass)) {
            return valueClass.cast(value.byteValue());
        } else if (Short.class.equals(valueClass)) {
            return valueClass.cast(value.shortValue());
        } else if (Integer.class.equals(valueClass) || int.class.equals(valueClass)) {
            return (R)Integer.valueOf(value.intValue());
        } else if (Long.class.equals(valueClass) || long.class.equals(valueClass)) {
            return (R)Long.valueOf(value.longValue());
        } else if (Float.class.equals(valueClass)) {
            return(R)Float.valueOf(value.floatValue());
        } else if (Double.class.equals(valueClass)) {
            return (R)Double.valueOf(value.doubleValue());
        } else {
            throw new IllegalArgumentException("Unsupported Number class: " + valueClass.getName());
        }
    }

    protected List<FT2<T,Integer>> rankingSameAsc(List<T> data, Comparator<T> comparator){
        return rankingSameAsc(data,comparator,data.size());
    }

    protected List<FT2<T,Integer>> rankingSameAsc(List<T> data, Comparator<T> comparator, int n) {
        if (data.isEmpty()){
            return Collections.emptyList();
        }
        if (n <= 0){
            throw new IllegalArgumentException("first N should greater than zero");
        }

        data.sort(comparator);
        int rank = 1;
        List<FT2<T,Integer>> tmpDataList = new ArrayList<>();
        tmpDataList.add(new FT2<>(data.get(0),1));
        for (int i = 1; i < data.size(); i++) {
            T pre = data.get(i-1);
            T cur = data.get(i);
            if (comparator.compare(pre,cur) != 0){
                rank += 1;
            }
            if (rank <= n){
                tmpDataList.add(new FT2<>(cur,rank));
            }else {
                break;
            }
        }
        return tmpDataList;
    }

}