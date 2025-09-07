import { privateApi } from "./core";

export const getMyAgreementsApi = (
  lastId?: number,
  role?: string,
  keyword?: string
) => {
    let url = "/agreements/summary?";
    if (lastId) {
        url += `lastId=${lastId}&`;
      }
      if (role) {
        url += `role=${role}&`;
      }
      if (keyword) {
        url += `keyword=${keyword}&`;
      }
      return privateApi.get(url);
};
