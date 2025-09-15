import { privateApi } from "./core";

export const getMyItemsApi = (
  lastId?: number,
  status?: string,
  keyword?: string
) => {
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

export const getItemDetailApi = (itemId: number) => {
  return privateApi.get(`/items/${itemId}/detail`);
};

export const getItemAgreementHistoryApi = (itemId: number, lastId?: number) => {
  let url = `/items/${itemId}/agreements?`;
  if (lastId) {
    url += `lastId=${lastId}&`;
  }
  return privateApi.get(url);
};

export const updateItemApi = (
  itemId: number,
  params: { title: string; description: string }
) => {
  return privateApi.put(`/items/${itemId}`, params);
};

export const updateItemImageApi = (itemId: number, formData: FormData) => {
  return privateApi.post(`/items/${itemId}/file`, formData, {
    headers: {
       "Content-Type": "multipart/form-data",
    },
  });
};
