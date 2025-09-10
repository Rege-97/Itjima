import { useEffect, useState } from "react";
import { Keyboard } from "react-native";
import { deleteUserApi, getProfileApi, updateProfile } from "../../../api/users";

type Profile = {
  id: number;
  name: string;
  email: string;
  phone?: string | null;
  createdAt: string;
  provider: "LOCAL" | "GOOGLE" | "KAKAO" | "NAVER" | string;
  providerId?: string | null;
};

const getErrMsg = (e: any) => {
  const data = e?.response?.data;
  if (typeof data === "string") return data;
  if (typeof data?.message === "string") return data.message;
  if (Array.isArray(data?.errors) && data.errors[0]?.message) return data.errors[0].message;
  return e?.message || "요청 처리 중 오류가 발생했습니다.";
};

export function useProfileScreen() {
  const [me, setMe] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const [phoneOpen, setPhoneOpen] = useState(false);
  const [pwOpen, setPwOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const [phone, setPhone] = useState("");
  const [savingPhone, setSavingPhone] = useState(false);

  const [currentPw, setCurrentPw] = useState("");
  const [newPw, setNewPw] = useState("");
  const [secureCurrent, setSecureCurrent] = useState(true);
  const [secureNew, setSecureNew] = useState(true);
  const [savingPw, setSavingPw] = useState(false);

  const [deleting, setDeleting] = useState(false);

  const [isKbOpen, setIsKbOpen] = useState(false);
  useEffect(() => {
    const s1 = Keyboard.addListener("keyboardDidShow", () => setIsKbOpen(true));
    const s2 = Keyboard.addListener("keyboardDidHide", () => setIsKbOpen(false));
    return () => {
      s1.remove();
      s2.remove();
    };
  }, []);

  const fetchMe = async () => {
    try {
      setLoading(true);
      const res = await getProfileApi();
      const data: Profile = res.data?.data ?? res.data;
      setMe(data);
      setPhone(data?.phone ?? "");
      setErr(null);
    } catch (e) {
      setErr(getErrMsg(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMe();
  }, []);

  const openPhoneModal = () => {
    setPhone(me?.phone ?? "");
    setPhoneOpen(true);
  };
  const dismissPhoneModal = () => {
    setPhoneOpen(false);
    setPhone(me?.phone ?? "");
    setSavingPhone(false);
  };

  const openPwModal = () => setPwOpen(true);
  const dismissPwModal = () => {
    setPwOpen(false);
    setCurrentPw("");
    setNewPw("");
    setSecureCurrent(true);
    setSecureNew(true);
    setSavingPw(false);
  };

  const dismissConfirmModal = () => {
    setConfirmOpen(false);
    setDeleting(false);
  };

  const handlePhoneBackdrop = () => {
    if (isKbOpen) {
      Keyboard.dismiss();
      return;
    }
    dismissPhoneModal();
  };
  const handlePwBackdrop = () => {
    if (isKbOpen) {
      Keyboard.dismiss();
      return;
    }
    dismissPwModal();
  };

  const onSavePhone = async () => {
    try {
      Keyboard.dismiss();
      if (!phone?.trim()) return;
      setSavingPhone(true);
      await updateProfile({ phone: phone.trim() });
      dismissPhoneModal();
      await fetchMe();
    } catch (e) {
      setSavingPhone(false);
      throw e;
    }
  };

  const onChangePassword = async () => {
    try {
      Keyboard.dismiss();
      if (!currentPw || !newPw) return;
      setSavingPw(true);
      await updateProfile({ currentPassword: currentPw, newPassword: newPw });
      dismissPwModal();
    } catch (e) {
      setSavingPw(false);
      throw e;
    }
  };

  const onDeleteAccount = async () => {
    try {
      setDeleting(true);
      await deleteUserApi();
      dismissConfirmModal();
    } catch (e) {
      setDeleting(false);
      throw e;
    }
  };

  const isKakao = me?.provider === "KAKAO";

  return {
    me,
    loading,
    err,
    fetchMe,
    phoneOpen,
    pwOpen,
    confirmOpen,
    openPhoneModal,
    dismissPhoneModal,
    openPwModal,
    dismissPwModal,
    dismissConfirmModal,
    handlePhoneBackdrop,
    handlePwBackdrop,
    phone,
    setPhone,
    savingPhone,
    currentPw,
    setCurrentPw,
    newPw,
    setNewPw,
    secureCurrent,
    setSecureCurrent,
    secureNew,
    setSecureNew,
    savingPw,
    deleting,
    isKbOpen,
    onSavePhone,
    onChangePassword,
    onDeleteAccount,
    getErrMsg,
    isKakao,
    setConfirmOpen,
  };
}
