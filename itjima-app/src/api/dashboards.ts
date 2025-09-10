import { privateApi } from "./core";

export const getDashboardApi = () => {
  return privateApi.get("/dashboards/summary");
};

export const getPendingAPi = () =>(
  lastId?: number
) => {
  let url = "/dashboards/pending?";
  if (lastId) {
    url += `lastId=${lastId}&`;
  }
  return privateApi.get(url);
}