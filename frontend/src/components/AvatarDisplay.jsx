function isImageUrl(src) {
  return /^(data:|https?:|\/|blob:)/.test(src);
}

export default function AvatarDisplay({ avatar, size = 40, className = '' }) {
  const isImage = isImageUrl(avatar ?? '');
  return (
    <div
      className={`rounded-full flex items-center justify-center overflow-hidden flex-shrink-0 ${className}`}
      style={{ width: size, height: size, background: isImage ? '#1e1b4b' : 'var(--color-primary)', fontSize: size * 0.5 }}
    >
      {isImage
        ? <img src={avatar} alt="avatar" className="w-full h-full object-cover" />
        : <span style={{ lineHeight: 1 }}>{avatar}</span>}
    </div>
  );
}