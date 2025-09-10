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

export const getProfileApi = () => {
  return privateApi.get("/users/me");
};

export const updateProfile = (params: {
  phone?: string;
  currentPassword?: string;
  newPassword?: string;
}) => {
  return privateApi.patch("/users/me", params);
};

export const deleteUserApi = () => {
  return privateApi.delete("/users/me");
};
