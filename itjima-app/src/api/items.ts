import { privateApi } from ".";

export const getMyItemApi = () => {
  return privateApi.get("/items");
};

export const createItemApi = (params: {
  type: "MONEY" | "OBJECT";
  title: string;
  description: string;
}) => {
  return privateApi.post("/items", params);
};
