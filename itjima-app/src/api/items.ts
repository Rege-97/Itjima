import { privateApi } from ".";

export const getMyItemsApi = (lastId?: number, status?: string, keyword?: string) => {
  let url = "/items/summary?";
  if (lastId) {
    url += `lastId=${lastId}&`;
  }
  if (status) {
    url += `status=${status}&`;
  }
  if (keyword) {
    url += `keyword=${keyword}&`;
  }
  return privateApi.get(url);
};

export const createItemApi = (params: {
  type: "MONEY" | "OBJECT";
  title: string;
  description: string;
}) => {
  return privateApi.post("/items", params);
};

export const getItemCountApi = () => {
  return privateApi.get("/items/count");
};
