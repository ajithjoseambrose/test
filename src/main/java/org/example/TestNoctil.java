package org.example;

public class TestNoctil {
    public void generateJsonBasedOnPosition(List<CSVRecord> records, JsonObject jsonObject, JsonArray jsonArray,
                                            List<Attributes> subAttributes, int distinctElementPosition, int distinctElementPositionSub,
                                            String distinctElementString) {
        ArrayList<String> filters = null;
        ArrayList<String> translitration = null;
        String distinctElementValueString = "";
        int childAttributePosition = 0;
        for (CSVRecord csvRecord : records) {
            if (csvRecord.get(distinctElementPositionSub).equals(distinctElementString)) {
                String valueFromCsv = csvRecord.get(distinctElementPosition);
                if (distinctElementValueString.equals("")) {
                    distinctElementValueString = valueFromCsv;
                }
                if (!distinctElementValueString.equals(valueFromCsv)) {
                    jsonArray.add(jsonObject);
                    jsonObject = new JsonObject();
                    distinctElementValueString = valueFromCsv;
                }
                for (Attributes attributeItem : subAttributes) {
                    JsonObject childJsonObject = new JsonObject();
                    JsonArray childJsonArray = new JsonArray();

                    if (attributeItem.getType().equals("String")) {
                        filters = (ArrayList<String>) attributeItem.getFilters();
                        String cellValue = null;
                        if (filters != null && !filters.isEmpty())
                            cellValue = (String) charFilter(attributeItem.getName(), filters, true);
                        translitration = (ArrayList<String>) attributeItem.getTranslitration();
                        if ( translitration != null) {
                            cellValue = (String) getTranslitratedValue(attributeItem.getName(), attributeItem);
                            LOGGER.debug("Translitration:" + cellValue + "Value: " + attributeItem.getTranslitration().size());
                        }

                        jsonObject.addProperty(cellValue, csvRecord.get(attributeItem.getPosition()));
                    } else if (attributeItem.getType().equals("Array")) {
                        List<Attributes> attributeList = attributeItem.getAttributes();
                        for (Attributes childAttributes : attributeList) {
                            if (childAttributes.isIdentifier()) {
                                childAttributePosition = childAttributes.getPosition();
                                generateJsonBasedOnPosition(records, childJsonObject, childJsonArray, attributeList,
                                        childAttributePosition, distinctElementPosition, distinctElementValueString);
                                jsonObject.add(attributeItem.getName(), childJsonArray);
                            }
                        }
                    } else if (attributeItem.getType().equals("Object")) {
                        List<Attributes> attributeList = attributeItem.getAttributes();
                        for (Attributes childAttributes : attributeList) {
                            childAttributePosition = childAttributes.getPosition();
                            generateJsonBasedOnPosition(records, childJsonObject, childJsonArray, attributeList,
                                    childAttributePosition, distinctElementPosition, distinctElementValueString);
                            jsonObject.add(attributeItem.getName(), childJsonObject);
                        }
                    } else if(attributeItem.getType().equals("SystemVariable")) {
                        jsonObject.add(attributeItem.getName(), null);
                        transformTask.createSource(attributeItem, jsonObject, request);
                    } else if(attributeItem.getType().equals("date")) {
                        String cellValue = processDate(attributeItem, csvRecord.get(attributeItem.getPosition()));
                        jsonObject.addProperty(attributeItem.getName(), cellValue);
                    } else if(attributeItem.getType().equals("Numeric")) {
                        String cellValue = csvRecord.get(attributeItem.getPosition());
                        try {
                            if (cellValue.contains("."))
                                jsonObject.addProperty(attributeItem.getName(), Double.parseDouble(cellValue));
                            else
                                jsonObject.addProperty(attributeItem.getName(), Integer.parseInt(cellValue));

                            LOGGER.info("Number Value:" + cellValue + " Value: " + jsonObject.get(attributeItem.getName()));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            LOGGER.info("Name:" + attributeItem.getName() + "Type:" + attributeItem.getType() + " Position: " + attributeItem.getPosition() +  " Value: " + cellValue);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        jsonArray.add(jsonObject);
        System.out.println("FROM THE NEW METHOD::: obj" + jsonObject.toString());
    }
}
