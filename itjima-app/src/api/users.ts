import { privateApi } from "./core";

export const searchUserByPhoneApi = (phone: string) => {
  return privateApi.get(`/users/search?phone=${phone}`);
};

export const recentPartnersApi = (lastId?: number) => {
  let url = "/users/recent-partners?";
  if (lastId) {
    url += `lastId=${lastId}&`;
  }
  return privateApi.get(url);
};
