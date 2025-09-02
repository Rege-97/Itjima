import { privateApi } from ".";

export const getMyItemsApi = (lastId?:number) => {
    const url = lastId ? `/items/summary?lastId=${lastId}` : "/items/summary";
  return privateApi.get(url);
};

export const createItemApi = (params: {
  type: "MONEY" | "OBJECT";
  title: string;
  description: string;
}) => {
  return privateApi.post("/items", params);
};
